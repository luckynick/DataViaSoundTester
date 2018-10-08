package com.luckynick.models;

import com.luckynick.CustomJFrame;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ExecutionException;

import static com.luckynick.custom.Utils.Log;

public class ModelEditor<T extends SerializableModel> extends CustomJFrame implements ActionListener, FocusListener {

    public static final String LOG_TAG = "PerformTests";

    private T editableModel;
    private boolean serialize = false;
    JLabel requiredFieldsLabel = new JLabel("<html><font color='red'>Fill in required fields!</font></html>");
    JLabel nothingToLoadLabel = new JLabel("<html><font color='red'>Nothing to load!</font></html>");

    private ModelIO<T> modelIO;

    public ModelEditor(T model, ModelIO<T> modelIO) {
        super("Edit " + model.nameOfModelClass);
        editableModel = model;
        this.modelIO = modelIO;

        addElements();
        displayWindow();
    }

    @Override
    protected void addElements() {
        for (String fieldName: this.editableModel.getConfigurableFieldNames()) {
            JPanel fieldPanel = new JPanel();
            JLabel label = new JLabel(fieldName);
            fieldPanel.add(label);

            Component inputField = editableModel.resolveSwingComponent(fieldName);
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
        JButton loadButton = new JButton("Load");
        loadButton.addActionListener(this);
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel);

        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        requiredFieldsLabel.setVisible(false);
        nothingToLoadLabel.setVisible(false);
        errorPanel.add(requiredFieldsLabel);
        errorPanel.add(nothingToLoadLabel);
        getContentPane().add(errorPanel);
    }

    /**
     *
     * @param model
     * @param <T>
     * @return null if serialization doesn't have to be done
     */
    public static <T extends SerializableModel> T requireEditedModel(T model, ModelIO<T> modelIO) {
        ModelEditor<T> manager = new ModelEditor<>(model, modelIO);
        SwingWorker<T, T> sw = new SwingWorker<T, T>() {

            @Override
            protected T doInBackground() throws Exception {
                Thread t = new Thread(manager, "ModelEditor thread");
                t.start();
                try {
                    t.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return manager.serialize ? manager.editableModel : null;
            }
        };
        sw.execute();
        try {
            return sw.get();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JButton) {
            if(e.getActionCommand().equals("Save")) {
                if(!editableModel.allRequiredFieldsAreSet()){
                    this.requiredFieldsLabel.setVisible(true);
                    refreshView();
                    return;
                }
                else
                    this.serialize = true;
                dispose();
            }
            else if (e.getActionCommand().equals("Load")) {
                File latestFile = Arrays.stream(new File(editableModel.fileRoot).listFiles())
                        .max((file, file2) -> {return file.compareTo(file2);}).orElse(null);
                if(latestFile == null) {
                    nothingToLoadLabel.setVisible(true);
                    refreshView();
                    return;
                }
                ModelIO<T> newModelIO = new ModelIO<>(modelIO.getClassOfModel(), latestFile);
                try {
                    editableModel = newModelIO.deserialize();
                }
                catch (IOException e1) {
                    System.out.println("Error during reading of profile.");
                    return;
                }
                modelIO = newModelIO;

                hardResetWindow();

                dispose();
            }
            else if (e.getActionCommand().equals("Cancel")) {
                dispose();
            }
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
        try {
            editableModel.setValue((Component) e.getSource());
        }
        catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
    }
}
