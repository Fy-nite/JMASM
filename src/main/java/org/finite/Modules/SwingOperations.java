package org.finite.Modules;

import org.finite.*;
import org.finite.ModuleManager.*;
import org.finite.ModuleManager.annotations.*;
import org.finite.Common.*;
import org.jetbrains.annotations.ApiStatus.Experimental;

import kotlin.ExperimentalStdlibApi;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@MNIClass("SwingOperations")
public class SwingOperations {
    private static final Map<Integer, JFrame> frames = new HashMap<>();
    private static final Map<Integer, JComponent> components = new HashMap<>();
    private static int nextId = 1;

    private static void setcallbackid(String callbackId) {
        // This method should be implemented to handle the callback ID
        // For now, it just prints the callback ID
        System.out.println("Callback ID: " + callbackId);
    }

    @MNIFunction(name = "createFrame", module = "SwingOperations")
    public static void createFrame(MNIMethodObject obj) {
        int id = nextId++;
        String title = obj.readString(obj.arg1);
        // register for outputing the frame
        JFrame frame = new JFrame(title);
        // don't close the window when the close button is pressed
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        // register a custom close operation
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent) {
                System.out.println("Window closed: " + title);
                Functions.hlt();

            }
        });
        frames.put(id, frame);
        System.out.println("Frame created with ID: " + id);
        obj.setRegister(obj.reg2, id);
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
        } else {
            System.out.println("Warning: Frame not found for ID: " + frameId);
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

    @MNIFunction(name = "setPosition", module = "SwingOperations")
    public static void setPosition(MNIMethodObject obj) {
        int componentId = obj.args[0];
        int x = obj.args[1];
        int y = obj.args[2];
        JComponent component = components.get(componentId);
        if (component != null) {
            component.setBounds(x, y, component.getWidth(), component.getHeight());
        }
    }

    @MNIFunction(name = "addClickListener", module = "SwingOperations")
    public static void addClickListener(MNIMethodObject obj) {
        int buttonId = obj.args[0]; // Button ID
        String callbackId = obj.argregs[1]; // Assembly callback function laben thingin
        JButton button = (JButton) components.get(buttonId);
        if (button != null) {
            button.addActionListener(e -> setcallbackid(callbackId));
        }
    }

    @MNIFunction(name = "setEnabled", module = "SwingOperations")
    public static void setEnabled(MNIMethodObject obj) {
        int componentId = obj.getRegister(obj.reg1);
        boolean enabled = obj.getRegister(obj.reg2) == 1;
        JComponent component = components.get(componentId);
        if (component != null) {
            component.setEnabled(enabled);
        }
    }

    @MNIFunction(name = "removeComponent", module = "SwingOperations")
    public static void removeComponent(MNIMethodObject obj) {
        int frameId = obj.getRegister(obj.reg1);
        int componentId = obj.getRegister(obj.reg2);
        JFrame frame = frames.get(frameId);
        JComponent component = components.get(componentId);
        if (frame != null && component != null) {
            frame.remove(component);
            components.remove(componentId);
        }
    }

    @MNIFunction(name = "showMessageDialog", module = "SwingOperations")
    public static void showMessageDialog(MNIMethodObject obj) {
        String message = obj.readString(obj.arg1);
        JOptionPane.showMessageDialog(null, message);
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

    @MNIFunction(name = "setLayout", module = "SwingOperations")
    public static void setLayout(MNIMethodObject obj) {
        int frameId = obj.getRegister(obj.reg1);
        int layoutType = obj.getRegister(obj.reg2);
        JFrame frame = frames.get(frameId);
        if (frame != null) {
            switch (layoutType) {
                case 0:
                    frame.setLayout(new FlowLayout());
                    break;
                case 1:
                    frame.setLayout(new BorderLayout());
                    break;
                case 2:
                    frame.setLayout(new GridLayout());
                    break;
                default:
                    System.out.println("Invalid layout type: " + layoutType);
            }
        } else {
            System.out.println("Frame not found for ID: " + frameId);
        }
    }

    @MNIFunction(name = "setVisible", module = "SwingOperations")
    public static void setVisible(MNIMethodObject obj) {
        int frameId = obj.getRegister(obj.reg1);
        boolean visible = obj.getRegister(obj.reg2) == 1;
        JFrame frame = frames.get(frameId);
        if (frame != null) {
            frame.setVisible(visible);
        } else {
            System.out.println("Frame not found for ID: " + frameId);
        }
    }

}
