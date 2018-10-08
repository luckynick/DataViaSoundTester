package com.luckynick.models;

import com.luckynick.CustomJFrame;
import com.luckynick.custom.Device;
import com.luckynick.shared.IOClassHandling;
import com.luckynick.shared.SharedUtils;

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
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
        List<T> modelObjects = modelIO.listObjects();
        /*String[] fileNames = new String[modelFiles.size()];
        for(int i = 0; i < fileNames.length; i++) {
            fileNames[i] = modelFiles.get(i).getName();
        }

        DefaultListModel<Item> listModel = new DefaultListModel<>();
        listModel.*/
        list = new JList(SharedUtils.toArray(modelObjects));
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
        nothingToLoadLabel.setVisible(modelObjects.size() == 0);
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
        SwingWorker<List<T>, List<T>> sw = new SwingWorker<List<T>, List<T>>() {

            @Override
            protected List<T> doInBackground() throws Exception {
                Thread t = new Thread(manager, "ModelEditor thread");
                t.start();
                try {
                    t.join();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return manager.selection;
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
        /*Thread t = new Thread(manager, "ModelSelector thread");
        t.start();
        try {
            t.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }
        return manager.selection;*/
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource() instanceof JButton) {
            if(e.getActionCommand().equals("Save")) {
                dispose();
            }
            else if(e.getActionCommand().equals("Cancel")) {
                selection = null;
                dispose();
            }
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            List<T> checked = list.getSelectedValuesList();
            System.out.println(checked.size() + " values selected");
            selection = checked;
        }
    }
}
