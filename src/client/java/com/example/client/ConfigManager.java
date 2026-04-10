package com.example.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "party_translator.json");

    public static String apiKey = "";
    public static int hudX = 10;
    public static int hudY = 10;
    public static float hudScale = 1.0f;

    public static void loadConfig() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json.has("isEnabled")) ExampleModClient.isEnabled = json.get("isEnabled").getAsBoolean();
                if (json.has("targetLanguage")) ExampleModClient.targetLanguage = json.get("targetLanguage").getAsString();
                if (json.has("translateMode")) ExampleModClient.translateMode = json.get("translateMode").getAsString();
                if (json.has("apiKey")) apiKey = json.get("apiKey").getAsString();
                if (json.has("hudX")) hudX = json.get("hudX").getAsInt();
                if (json.has("hudY")) hudY = json.get("hudY").getAsInt();
                if (json.has("hudScale")) hudScale = json.get("hudScale").getAsFloat();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            saveConfig();
        }
    }

    public static void saveConfig() {
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            JsonObject json = new JsonObject();
            json.addProperty("isEnabled", ExampleModClient.isEnabled);
            json.addProperty("targetLanguage", ExampleModClient.targetLanguage);
            json.addProperty("translateMode", ExampleModClient.translateMode);
            json.addProperty("apiKey", apiKey);
            json.addProperty("hudX", hudX);
            json.addProperty("hudY", hudY);
            json.addProperty("hudScale", hudScale);
            GSON.toJson(json, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
