package org.finite.Modules.extentions;

import org.finite.ModuleManager.*;
import org.finite.ModuleManager.annotations.*;
import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@MNIClass("SwingOperations")
public class SwingOperations {
    private static final Map<Integer, JFrame> frames = new HashMap<>();
    private static final Map<Integer, JComponent> components = new HashMap<>();
    private static int nextId = 1;

    @MNIFunction(name = "createFrame", module = "SwingOperations")
    public static void createFrame(MNIMethodObject obj) {
        int id = nextId++;
        String title = obj.readString(obj.arg1);
        JFrame frame = new JFrame(title);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frames.put(id, frame);
        obj.setRegister(obj.reg1, id);
    }

    @MNIFunction(name = "showFrame", module = "SwingOperations")
    public static void showFrame(MNIMethodObject obj) {
        int frameId = obj.getRegister(obj.reg1);
        JFrame frame = frames.get(frameId);
        if (frame != null) {
            frame.pack();
            frame.setVisible(true);
        }
    }

    @MNIFunction(name = "setFrameSize", module = "SwingOperations") 
    public static void setFrameSize(MNIMethodObject obj) {
        int frameId = obj.getRegister(obj.reg1);
        int width = obj.getRegister(obj.reg2);
        int height = 500;
        JFrame frame = frames.get(frameId);
        if (frame != null) {
            frame.setSize(width, height);
            frame.setPreferredSize(new Dimension(width, height));
            frame.setMinimumSize(new Dimension(width, height));
            System.out.println("Frame size set to: " + width + "x" + height);
        }
        else 
        {
            System.out.println("Warning: Frame not found");
        }
    }

    @MNIFunction(name = "createButton", module = "SwingOperations")
    public static void createButton(MNIMethodObject obj) {
        int id = nextId++;
        String text = obj.readString(obj.arg1);
        JButton button = new JButton(text);
        components.put(id, button);
        obj.setRegister(obj.reg1, id);
    }

    @MNIFunction(name = "createLabel", module = "SwingOperations")
    public static void createLabel(MNIMethodObject obj) {
        int id = nextId++;
        String text = obj.readString(obj.arg1);
        int frameId = obj.getRegister(obj.reg2);
        JFrame frame = frames.get(frameId);
        JLabel label = new JLabel(text);
        components.put(id, label);
        // set the component to the register
        obj.setRegister(obj.reg1, id);
    }

    @MNIFunction(name = "createTextField", module = "SwingOperations")
    public static void createTextField(MNIMethodObject obj) {
        int id = nextId++;
        JTextField textField = new JTextField(20);
        components.put(id, textField);
        obj.setRegister(obj.reg1, id);
    }

    @MNIFunction(name = "addToFrame", module = "SwingOperations")
    public static void addToFrame(MNIMethodObject obj) {
        int frameId = obj.getRegister(obj.reg1);
        int componentId = obj.getRegister(obj.reg2);
        JFrame frame = frames.get(frameId);
        JComponent component = components.get(componentId);
        if (frame != null && component != null) {
            frame.add(component);
        }
    }

    @MNIFunction(name = "setText", module = "SwingOperations")
    public static void setText(MNIMethodObject obj) {
        int componentId = obj.getRegister(obj.reg1);
        String text = obj.readString(obj.arg2);
        JComponent component = components.get(componentId);
        if (component instanceof JLabel) {
            ((JLabel) component).setText(text);
        } else if (component instanceof JButton) {
            ((JButton) component).setText(text);
        } else if (component instanceof JTextField) {
            ((JTextField) component).setText(text);
        }
    }

    @MNIFunction(name = "getText", module = "SwingOperations")
    public static void getText(MNIMethodObject obj) {
        int componentId = obj.getRegister(obj.reg1);
        JComponent component = components.get(componentId);
        String text = "";
        if (component instanceof JLabel) {
            text = ((JLabel) component).getText();
        } else if (component instanceof JButton) {
            text = ((JButton) component).getText();
        } else if (component instanceof JTextField) {
            text = ((JTextField) component).getText();
        }
        // obj.setRegister(obj.reg1, text);
    }

    @MNIFunction(name = "disposeFrame", module = "SwingOperations")
    public static void disposeFrame(MNIMethodObject obj) {
        int frameId = obj.getRegister(obj.reg1);
        JFrame frame = frames.get(frameId);
        if (frame != null) {
            frame.dispose();
            frames.remove(frameId);
        }
    }
}
