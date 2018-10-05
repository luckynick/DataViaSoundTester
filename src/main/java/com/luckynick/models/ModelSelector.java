package com.luckynick.models;

import com.luckynick.CustomJFrame;
import com.luckynick.custom.Device;
import com.luckynick.shared.IOClassHandling;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ModelSelector<T extends SerializableModel> extends CustomJFrame implements ActionListener, ListSelectionListener {

    private List<T> selection = new ArrayList<>();
    private boolean multipleSelection;
    JLabel nothingToLoadLabel = new JLabel("<html><font color='red'>Nothing to load!</font></html>");
    private JList list;
    private JScrollPane spane;

    private ModelIO<T> modelIO;
    private Class<T> modelClass;

    public ModelSelector(ModelIO<T> modelIO) {
        this(modelIO, false);
    }

    public ModelSelector(ModelIO<T> modelIO, boolean multipleSelection) {
        super("Select " + modelIO.getClassOfModel().getSimpleName());
        this.multipleSelection = multipleSelection;
        this.modelIO = modelIO;
        this.modelClass = modelIO.getClassOfModel();

        addElements();
        displayWindow();
    }

    @Override
    protected void addElements() {
        List<File> modelFiles = modelIO.listFiles();
        String[] fileNames = new String[modelFiles.size()];
        for(int i = 0; i < fileNames.length; i++) {
            fileNames[i] = modelFiles.get(i).getName();
        }

        list = new JList(fileNames);
        list.addListSelectionListener(this);

        spane = new JScrollPane();
        spane.getViewport().add(list);

        getContentPane().add(spane);

        /*try {
            for (File modelFile: modelFiles) {
                T stored = modelIO.deserialize(modelFile);

                JPanel fieldPanel = new JPanel();
                JLabel label = new JLabel(stored.filename);
                fieldPanel.add(label);

                fieldPanel.setBorder(new CompoundBorder(label.getBorder(), new EmptyBorder(10,10,10,10)));
                getContentPane().add(fieldPanel);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }*/

        JPanel buttonPanel = new JPanel();
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(this);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(this);
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        getContentPane().add(buttonPanel);

        JPanel errorPanel = new JPanel();
        errorPanel.setLayout(new BoxLayout(errorPanel, BoxLayout.Y_AXIS));
        nothingToLoadLabel.setVisible(modelFiles.size() == 0);
        errorPanel.add(nothingToLoadLabel);
        getContentPane().add(errorPanel);
    }



    /**
     *
     * @param <T>
     * @return null if serialization doesn't have to be done
     */
    public static <T extends SerializableModel> List<T> requireSelection(ModelIO<T> modelIO, boolean multipleSelection) {
        ModelSelector<T> manager = new ModelSelector<>(modelIO, multipleSelection);
        Thread t = new Thread(manager, "ModelSelector thread");
        t.start();
        try {
            t.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return manager.selection;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JButton) {
            if(e.getActionCommand().equals("Save")) {

            }
            else if(e.getActionCommand().equals("Cancel")) {
                selection = null;
            }
            dispose();
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            List<String> ckecked = list.getSelectedValuesList();

            System.out.println(ckecked.size() + " values selected");
        }
    }
}
