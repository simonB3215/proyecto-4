package com.example.client.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class HypixelApiHelper {
    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static CompletableFuture<String> syncLanguageFromHypixel(String apiKey) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                if (MinecraftClient.getInstance().player == null) return null;
                String uuid = MinecraftClient.getInstance().player.getUuidAsString().replace("-", "");
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://api.hypixel.net/v2/player?uuid=" + uuid))
                        .header("API-Key", apiKey)
                        .GET()
                        .build();

                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
                    if (json.has("success") && json.get("success").getAsBoolean() && json.has("player") && !json.get("player").isJsonNull()) {
                        JsonObject player = json.getAsJsonObject("player");
                        if (player.has("userLanguage")) {
                            String lang = player.get("userLanguage").getAsString().toLowerCase();
                            switch (lang) {
                                case "spanish": return "es";
                                case "english": return "en";
                                case "french": return "fr";
                                case "german": return "de";
                                case "polish": return "pl";
                                case "portuguese": return "pt";
                                case "russian": return "ru";
                                case "italian": return "it";
                                default: return "en";
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        });
    }
}
