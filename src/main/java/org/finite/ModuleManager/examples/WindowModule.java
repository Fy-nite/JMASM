package org.finite.ModuleManager.examples;

import org.finite.ModuleManager.annotations.MNIClass;
import org.finite.ModuleManager.annotations.MNIFunction;
import org.finite.ModuleManager.MNIMethodObject;
import javax.swing.*;
import java.util.HashMap;

@MNIClass("window")
public class WindowModule {
    private static final HashMap<Integer, JFrame> windows = new HashMap<>();
    private static final HashMap<Integer, JComponent> components = new HashMap<>();
    private static int nextWindowId = 0;
    private static int nextComponentId = 0;

    @MNIFunction(module = "window", name = "create")
    public static void createWindow(MNIMethodObject obj) {
        // First arg: title address, Second arg: store window ID
        String title = readString(obj, obj.getRegister(obj.reg1));
        JFrame frame = new JFrame(title);
        frame.setSize(400, 300);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null); // Use absolute positioning
        int windowId = nextWindowId++;
        windows.put(windowId, frame);
        obj.setRegister(obj.reg2, windowId);
    }

    @MNIFunction(module = "window", name = "show")
    public static void showWindow(MNIMethodObject obj) {
        // First arg: window ID
        int windowId = obj.getRegister(obj.reg1);
        JFrame frame = windows.get(windowId);
        if (frame != null) {
            frame.setVisible(true);
        }
    }

    @MNIFunction(module = "window", name = "button")
    public static void createButton(MNIMethodObject obj) {
        // reg1: window ID, reg2: store component ID
        // Memory at reg1+1: button text
        // Memory at reg1+2,3,4,5: x,y,width,height
        int windowId = obj.getRegister(obj.reg1);
        JFrame frame = windows.get(windowId);
        if (frame != null) {
            String text = readString(obj, obj.getRegister(obj.reg1) + 1);
            int x = obj.readMemory(obj.getRegister(obj.reg1) + 2);
            int y = obj.readMemory(obj.getRegister(obj.reg1) + 3);
            int width = obj.readMemory(obj.getRegister(obj.reg1) + 4);
            int height = obj.readMemory(obj.getRegister(obj.reg1) + 5);
            
            JButton button = new JButton(text);
            button.setBounds(x, y, width, height);
            frame.add(button);
            
            int componentId = nextComponentId++;
            components.put(componentId, button);
            obj.setRegister(obj.reg2, componentId);
            frame.repaint();
        }
    }

    @MNIFunction(module = "window", name = "label")
    public static void createLabel(MNIMethodObject obj) {
        // Same layout as button
        int windowId = obj.getRegister(obj.reg1);
        JFrame frame = windows.get(windowId);
        if (frame != null) {
            String text = readString(obj, obj.getRegister(obj.reg1) + 1);
            int x = obj.readMemory(obj.getRegister(obj.reg1) + 2);
            int y = obj.readMemory(obj.getRegister(obj.reg1) + 3);
            int width = obj.readMemory(obj.getRegister(obj.reg1) + 4);
            int height = obj.readMemory(obj.getRegister(obj.reg1) + 5);
            
            JLabel label = new JLabel(text);
            label.setBounds(x, y, width, height);
            frame.add(label);
            
            int componentId = nextComponentId++;
            components.put(componentId, label);
            obj.setRegister(obj.reg2, componentId);
            frame.repaint();
        }
    }

    private static String readString(MNIMethodObject obj, int address) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (true) {
            int ch = obj.readMemory(address + i);
            if (ch == 0) break;
            sb.append((char)ch);
            i++;
        }
        return sb.toString();
    }
}
