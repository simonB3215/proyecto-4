package com.example.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class TranslatorHelper {

    private static final HttpClient CLIENT = HttpClient.newHttpClient();

    public static CompletableFuture<String> translateAsync(String text, String targetLang) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
                // Utilizando el targetLang de forma dinámica
                String urlStr = "https://translate.googleapis.com/translate_a/single?client=gtx&sl=auto&tl=" + targetLang + "&dt=t&q=" + encodedText;
                
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(urlStr))
                        .header("User-Agent", "Mozilla/5.0")
                        .GET()
                        .build();

                HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

                if (response.statusCode() == 200) {
                    JsonArray jsonArray = JsonParser.parseString(response.body()).getAsJsonArray();
                    JsonArray parts = jsonArray.get(0).getAsJsonArray();
                    StringBuilder translatedText = new StringBuilder();
                    for (JsonElement part : parts) {
                        translatedText.append(part.getAsJsonArray().get(0).getAsString());
                    }
                    return translatedText.toString();
                } else {
                    return "[Error de red: " + response.statusCode() + "]";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "[Error de traducción]";
            }
        });
    }
}

