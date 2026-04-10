package com.example.client;

import net.minecraft.client.KeyMapping;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.io.FileWriter;

public class Reflector {
    public static void run() {
        try (FileWriter fw = new FileWriter("reflector_out.txt")) {
            for (Constructor<?> c : KeyMapping.class.getDeclaredConstructors()) {
                fw.write("Constructor: " + c.toString() + "\n");
            }
            for (Method m : KeyMapping.class.getDeclaredMethods()) {
                fw.write("Method: " + m.toString() + "\n");
            }
        } catch (Exception e) {}
    }
}
