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

    public static boolean isTranslatingOutput = false;
    public static boolean isRepopulating = false;
    
    // Almacenamiento de historiales de canales de chat (máximo 100 mensajes)
    public static java.util.List<Text> allMessages = new java.util.ArrayList<>();
    public static java.util.List<Text> translatedMessages = new java.util.ArrayList<>();
    public static int currentTab = 0; // 0 = Todos, 1 = Traducciones

    @Override
    public void onInitializeClient() {
        ConfigManager.loadConfig();

        KeyBinding.Category CATEGORY = new KeyBinding.Category(net.minecraft.util.Identifier.of("translator", "main"));
        KeyBinding openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding("key.translator.open_menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) client.setScreen(new TranslatorOptionsScreen(client.currentScreen));
        });

        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(com.example.client.gui.PartyChatHud::render);

        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String version = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("modid").map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("1.0.0");
            client.execute(() -> {
                if (client.inGameHud != null && isEnabled && !client.isInSingleplayer()) {
                    client.inGameHud.getChatHud().addMessage(Text.literal("§8[§bPartyTranslator§8] §7» §aFuncionando correctamente. §7Versión: §e" + version));
                }
            });
        });

        // Botones de canales en la parte superior izquierda de la pantalla de chat
        net.fabricmc.fabric.api.client.screen.v1.ScreenEvents.AFTER_INIT.register((clientInstance, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof net.minecraft.client.gui.screen.ChatScreen) {
                int tabY = 4; // Borde superior de la pantalla
                
                net.fabricmc.fabric.api.client.screen.v1.Screens.getButtons(screen).add(
                    net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal(currentTab == 0 ? "§a▶ Todos" : "Todos"), btn -> {
                        switchTab(0, clientInstance);
                        clientInstance.setScreen(screen);
                    }).dimensions(5, tabY, 60, 20).build()
                );

                net.fabricmc.fabric.api.client.screen.v1.Screens.getButtons(screen).add(
                    net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal(currentTab == 1 ? "§a▶ Traducido" : "Traducido"), btn -> {
                        switchTab(1, clientInstance);
                        clientInstance.setScreen(screen);
                    }).dimensions(70, tabY, 80, 20).build()
                );

                net.fabricmc.fabric.api.client.screen.v1.Screens.getButtons(screen).add(
                    net.minecraft.client.gui.widget.ButtonWidget.builder(Text.literal("⚙ Conf"), btn -> {
                        clientInstance.setScreen(new TranslatorOptionsScreen(screen));
                    }).dimensions(155, tabY, 50, 20).build()
                );
            }
        });
    }

    public static void switchTab(int tabIndex, MinecraftClient client) {
        currentTab = tabIndex;
        if (client.inGameHud == null || client.inGameHud.getChatHud() == null) return;
        
        client.inGameHud.getChatHud().clear(false); // Limpia los mensajes en pantalla
        isRepopulating = true;
        
        java.util.List<Text> targetList = (currentTab == 0) ? allMessages : translatedMessages;
        for (Text t : targetList) {
            client.inGameHud.getChatHud().addMessage(t);
        }
        
        isRepopulating = false;
    }

    public static boolean shouldCancelAndTranslate(Text messageObj, String rawText) {
        if (isRepopulating || isTranslatingOutput) return false;

        // Registrar en canal "Todos" (excepto si es nuestra propia inyección asincrónica)
        allMessages.add(messageObj);
        if (allMessages.size() > 100) allMessages.remove(0);

        if (!isEnabled) return currentTab == 1; // Si está desactivado pero en tab 1, cancelamos todo para que no vea nada? Mejor retornamos true si tab=1

        if (translateMode.equals("PARTY") && (rawText.contains("Party") || rawText.contains("Grupo"))) {
            Matcher matcher = PARTY_CHAT_PATTERN.matcher(rawText);
            if (matcher.matches()) {
                executeTranslation(matcher.group(1), matcher.group(2), matcher.group(3));
                return ConfigManager.combinedMode || currentTab == 1;
            }
        } else if (translateMode.equals("ALL")) {
            Matcher matcher = ALL_CHAT_PATTERN.matcher(rawText);
            if (matcher.matches()) {
                executeTranslation("", matcher.group(1), matcher.group(2));
                return ConfigManager.combinedMode || currentTab == 1;
            }
        }
        
        // Si no se tradujo, no mostrarlo si estamos en el canal 1 (Traducido)
        return currentTab == 1;
    }

    private static void executeTranslation(String prefix, String userPart, String textToTranslate) {
        TranslatorHelper.translateAsync(textToTranslate, targetLanguage).thenAccept(translatedText -> {
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.inGameHud != null) {
                client.execute(() -> {
                    Text vanilatxt = Text.literal("§9" + prefix + "§f" + userPart + ConfigManager.textColor + translatedText);
                    
                    translatedMessages.add(vanilatxt);
                    if (translatedMessages.size() > 100) translatedMessages.remove(0);
                    
                    // Si CombinedMode está activado, también se inyecta al canal Todos
                    if (ConfigManager.combinedMode) {
                        allMessages.add(vanilatxt);
                        if (allMessages.size() > 100) allMessages.remove(0);
                    }
                    
                    // Solo renderizar si estamos en la pestaña apropiada
                    boolean shouldRender = false;
                    if (currentTab == 0 && ConfigManager.combinedMode) shouldRender = true;
                    if (currentTab == 1) shouldRender = true;

                    if (shouldRender) {
                        isTranslatingOutput = true;
                        client.inGameHud.getChatHud().addMessage(vanilatxt);
                        isTranslatingOutput = false;
                    } 
                    
                    // Y siempre enviarlo al Overlay si CombinedMode es falso
                    if (!ConfigManager.combinedMode) {
                        com.example.client.gui.PartyChatHud.addMessage(vanilatxt);
                    }
                });
            }
        });
    }
}