package com.example.client;

import net.minecraft.client.gui.widget.ClickableWidget;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

public class TestDumper {
    public static void dump() {
        System.out.println("METHODS IN CLICKABLEWIDGET:");
        for (Method m : ClickableWidget.class.getMethods()) {
            System.out.println("METHOD: " + m.getName() + " ARGS: " + Arrays.stream(m.getParameterTypes()).map(Class::getName).collect(Collectors.joining(", ")));
        }
    }
}
