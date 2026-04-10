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

    private static final Pattern PARTY_CHAT_PATTERN = Pattern.compile(".*?((?:Party|Grupo).*?>\\s*)(.*?:\\s*)(.*)", Pattern.DOTALL);
    private static final Pattern ALL_CHAT_PATTERN = Pattern.compile("^(.*?:\\s+)(.*)$", Pattern.DOTALL);

    // Variables de estado
    public static boolean isEnabled = true;
    public static String targetLanguage = "es";
    public static String translateMode = "PARTY"; // "PARTY" o "ALL"

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

        // Mensaje de bienvenida al unirse a un servidor o mundo
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String version = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("modid")
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("1.0.0");
            
            client.execute(() -> {
                if (client.inGameHud != null && isEnabled && !client.isInSingleplayer()) {
                    client.inGameHud.getChatHud().addMessage(
                        Text.literal("§8[§bPartyTranslator§8] §7» §aFuncionando correctamente. §7Versión: §e" + version)
                    );
                }
            });
        });
    }

    public static void checkPartyChat(String rawText) {
        if (!isEnabled) return;
        processMessage(rawText);
    }

    private static void processMessage(String rawText) {
        if (translateMode.equals("PARTY")) {
            if ((rawText.contains("Party") || rawText.contains("Grupo"))) {
                Matcher matcher = PARTY_CHAT_PATTERN.matcher(rawText);
                if (matcher.matches()) {
                    executeTranslation(matcher.group(1), matcher.group(2), matcher.group(3));
                }
            }
        } else if (translateMode.equals("ALL")) {
            Matcher matcher = ALL_CHAT_PATTERN.matcher(rawText);
            if (matcher.matches()) {
                // Para ALL mode, la regex simplificada divide en (Prefijo+Usuario:) y (Mensaje)
                executeTranslation("", matcher.group(1), matcher.group(2));
            }
        }
    }

    private static void executeTranslation(String prefix, String userPart, String textToTranslate) {
        TranslatorHelper.translateAsync(textToTranslate, targetLanguage).thenAccept(translatedText -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.inGameHud != null) {
                client.execute(() -> {
                    Text vanilatxt = Text.literal("§9" + prefix + "§f" + userPart + "§e" + translatedText);
                    com.example.client.gui.PartyChatHud.addMessage(vanilatxt);
                });
            }
        });
    }
}