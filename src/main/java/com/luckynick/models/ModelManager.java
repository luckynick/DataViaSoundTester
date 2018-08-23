package com.luckynick.models;

import com.luckynick.PureFunctionalInterface;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class ModelManager<T extends SerializableModel> extends JFrame implements Runnable, ActionListener, FocusListener {

    Thread thisThread;
    PureFunctionalInterface onWindowDispatch;
    private T editableModel;
    private boolean serialize = false;

    public ModelManager(T model) {
        super("Edit model");
        editableModel = model;

        // Configure window content
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                dispose();
            }
        });

        for (String fieldName: model.getAllowedFieldNamesArray()) {
            JPanel fieldPanel = new JPanel();
            JLabel label = new JLabel(fieldName);
            fieldPanel.add(label);
            Object value = model.getValue(fieldName);

            Component inputField = model.resolveSwingComponent(fieldName);
            inputField.addFocusListener(this);
            fieldPanel.add(inputField);

            fieldPanel.setBorder(new CompoundBorder(label.getBorder(), new EmptyBorder(10,10,10,10)));
            getContentPane().add(fieldPanel);
        }

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel);

        // Launch the window
        pack();
        setVisible(true);
        setExtendedState(JFrame.ICONIFIED);
        setExtendedState(JFrame.NORMAL);
    }

    /**
     *
     * @param model
     * @param <T>
     * @return null if serialization doesn't have to be done
     */
    public static <T extends SerializableModel> T requireEditedModel(T model) {
        ModelManager<T> manager = new ModelManager<>(model);
        Thread t = new Thread(manager, "ModelManager thread");
        t.start();
        try {
            t.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return manager.serialize ? manager.editableModel : null;
    }

    @Override
    public void run() {
        synchronized (this) {
            try {
                thisThread = Thread.currentThread();
                wait();
            }
            catch (InterruptedException e) {
                System.out.println(thisThread.getName() + " was stopped.");
            }
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        thisThread.interrupt();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        System.out.println(e.getSource().getClass().getSimpleName() + " " + e.getActionCommand());
        if(e.getSource() instanceof JButton) {
            if(e.getActionCommand().equals("Save")) {
                this.serialize = true;
            }
            dispose();
        }
        else if(e.getSource() instanceof JTextField) {
            System.out.println("Field updated: " + ((JTextField) e.getSource()).getText());
            String textFieldValue = ((JTextField) e.getSource()).getText();
            Object newValue = textFieldValue.equals("") ? null : textFieldValue;
            editableModel.setValue(e.getActionCommand(), newValue);
        }
    }

    @Override
    public void focusGained(FocusEvent e) {

    }

    @Override
    public void focusLost(FocusEvent e) {
        System.out.println("Focus lost: " + ((Component) e.getSource()).getName());
        editableModel.setValue((Component) e.getSource());
        //((JTextField) e.getSource()).postActionEvent();
    }
}
